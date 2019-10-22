package com.motif.motif.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.motif.motif.Constants;
import com.motif.motif.entities.DotNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by motif on 2/5/2018.
 */

public class Loader{

    private static final String TAG = Loader.class.getSimpleName();


    private Cursor mNodeCursor = null;
    private MotifDatabaseHelper mMotifDatabaseHelper;


    private Context mContext;

    private int mCount;
    private int mGraphNumber;

    private boolean mLoadAll = true;

    private LoaderListener mLoaderListener =  null;

    public Loader(Context context, int count){
        mGraphNumber = 1; // default value
        mContext = context;
        mCount = count;

        // OPEN DATABASE
        try {
            mMotifDatabaseHelper = MotifDatabaseHelper.getInstance(mContext);
            mMotifDatabaseHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        finally {
            try {
                mMotifDatabaseHelper.openDataBase();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets the graph to be read
     * @param graphNumber
     */
    public void setGraphNumber(int graphNumber){
        mGraphNumber = graphNumber;
    }


    /**
     * Starts the NodesLoaderTask that reads and loads from database graph nodes
     */
    public void loadNodes(){
        new NodesLoaderTask().execute();
    }


    /**
     * Checks edgeMap for the value of an edge.
     * An edge is represented as an ArrayList of the IDs of the source and target nodes
     * If the key doesn't exist in the HashMap, edgeMap, the database is queried to find the
     * corresponding value. True if an edge exists in the database; otherwise, false.
     *
     * @param node1 the ID of the 1st node
     * @param node2 the ID of the other (2nd) node
     * @param edgeMap
     * @return
     */
    public boolean checkEdge(int node1, int node2, HashMap<ArrayList, Boolean> edgeMap){
        String edgeTableName = String.format("motifdata%02d_edges", mGraphNumber);

        int srcID = node1;
        int dstID = node2;
        boolean edgeExists = false;

        if(srcID > dstID) {
            int temp = srcID;
            srcID = dstID;
            dstID = temp;
        }

        ArrayList key = new ArrayList(2);
        key.add(srcID); // source node
        key.add(dstID); // target node

        if(edgeMap.containsKey(key)){
            return (boolean)edgeMap.get(key);
        }

        //=================================================================
        // GET EDGE
        //=================================================================
        String query = "SELECT * from " + edgeTableName +
                " WHERE source== " +  String.valueOf(srcID) +
                " and target == " + String.valueOf(dstID) + ";";

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());

        Cursor edgeCursor = mMotifDatabaseHelper.rawQuery(query, null);
        if(edgeCursor.moveToFirst()) {
            if(edgeCursor.getInt(MotifDatabaseHelper.KEY_WEIGHT) >= Constants.DIFFICULTY_MULTIPLIER *
                    pref.getFloat(Constants.KEY_EDGE_THRESHOLD, Constants.VALUE_NORMAL)) {
                edgeExists = true;
            }
        }

        //=================================================================
        // CACHE EDGE
        //=================================================================
        edgeMap.put(key, Boolean.valueOf(edgeExists));

        edgeCursor.close();
        return edgeExists;
    }


    public void close(){
        if(mMotifDatabaseHelper != null) mMotifDatabaseHelper.close();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Async Task for Handling DB loading
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class NodesLoaderTask extends AsyncTask<String[], ArrayList<DotNode> , ArrayList<DotNode> > {

        @Override
        protected void onPostExecute(ArrayList<DotNode> values) {
            super.onPostExecute(values);
            if(mLoadAll){
                if(mLoaderListener != null)
                    mLoaderListener.onLoadBuffer(new ArrayList<DotNode>(values));
            }
        }

        @Override
        protected void onProgressUpdate(ArrayList<DotNode>... values) {
            super.onProgressUpdate(values);
            if(mLoaderListener != null) {
                ArrayList<DotNode> list = new ArrayList<>(values[0]);
                Collections.shuffle(list);
                mLoaderListener.onLoad(list);
            }
        }

        @Override
        protected ArrayList<DotNode> doInBackground(String[]... params) {
            ArrayList<DotNode> startingNodes = new ArrayList<>();
            ArrayList<DotNode> allNodes = new ArrayList<>();


            if(mNodeCursor == null){
                String nodeTableName = String.format("motifdata%02d_nodes", mGraphNumber);
                Log.i(TAG, "NODE LOADER CALLED");

                if(params.length < 1){
                    mNodeCursor = mMotifDatabaseHelper.query(nodeTableName, null, null,
                            null, "RANDOM()", null, null);
                }else{
                    String selection = "id IN (?";
                    for(int i = 1; i < params[0].length; i++)
                        selection += ", ?";
                    selection += " )";
                    mNodeCursor = mMotifDatabaseHelper.query(nodeTableName, null,
                            selection,
                            params[0], null, null, null);
                    mCount = params[0].length;
                }

            }

            if (mNodeCursor.moveToFirst()) {
                DotNode node;

                //============================================
                // LOAD NODES
                //============================================
                int i = 0;
                do {
                    node = new DotNode(
                            mNodeCursor.getInt(MotifDatabaseHelper.KEY_ID),
                            mNodeCursor.getString(MotifDatabaseHelper.KEY_LABEL),
                            mNodeCursor.getInt(MotifDatabaseHelper.KEY_DEGREE));

                    if(mLoaderListener != null && mLoaderListener.isNodeValid(node.degree)){
                        startingNodes.add(node);
                        i++;
                    }

                    allNodes.add(node);

                    if(i == mCount){
                        // Publish "mCount" nodes for initializing game surface
                        publishProgress(startingNodes);
                    }

                } while (mNodeCursor.moveToNext());

            }
            return allNodes;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Interface for accessing Loaded data
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     *  Methods of the LoaderListener interface are called during data loading and immediately
     *  after data has been loaded from the database.
     */
    public interface LoaderListener{
        boolean isNodeValid(int degree);
        void onLoad(ArrayList<DotNode> nodes);
        void onLoadBuffer(ArrayList<DotNode> nodes);
    }

    public void setLoadListener(LoaderListener listener){
        this.mLoaderListener = listener;
    }
}