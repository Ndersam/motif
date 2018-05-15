package com.nders.motif.data;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.AsyncTask;
import android.util.Log;

import com.nders.motif.Constants;
import com.nders.motif.entities.DotNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nders on 2/5/2018.
 */

public class Loader{

    private static final String TAG = Loader.class.getSimpleName();


    private Cursor mNodeCursor = null;
    private MotifDatabaseHelper mMotifDatabaseHelper;


    private Context mContext;

    private int mCount;
    private int mBufferCount;
    private int mGraphNumber;

    private LoaderListener mLoaderListener =  null;

    public Loader(Context context, int count){
        mGraphNumber = 1; // default value
        mContext = context;
        mCount = count;
        mBufferCount = Constants.MAX_NODE_COUNT - count;

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

        //Cursor edgeCursor = mSQLiteDB.rawQuery(query, null);
        Cursor edgeCursor = mMotifDatabaseHelper.rawQuery(query, null);
        if(edgeCursor.moveToFirst()) {
            if(edgeCursor.getInt(MotifDatabaseHelper.KEY_WEIGHT) >= Constants.EDGE_THRESHOLD) {
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

    private class NodesLoaderTask extends AsyncTask<Void, ArrayList<DotNode> , ArrayList<DotNode> > {

        @Override
        protected void onPostExecute(ArrayList<DotNode> values) {
            super.onPostExecute(values);
            if(mLoaderListener != null) mLoaderListener.onLoadBuffer(values);
        }

        @Override
        protected void onProgressUpdate(ArrayList<DotNode>... values) {
            super.onProgressUpdate(values);
            if(mLoaderListener != null) mLoaderListener.onLoad(values[0]);
        }

        @Override
        protected ArrayList<DotNode> doInBackground(Void... voids) {
            ArrayList<DotNode> dotNodes = new ArrayList<>();
            ArrayList<DotNode> bufferNodes = new ArrayList<>();


            if(mNodeCursor == null){
                String nodeTableName = String.format("motifdata%02d_nodes", mGraphNumber);
                String query = "SELECT * FROM " + nodeTableName + " WHERE mId IN (SELECT mId FROM "
                        + nodeTableName + " ORDER BY RANDOM() )"; //LIMIT " + mCount + "
                Log.i(TAG, "NODE LOADER CALLED");
                //mNodeCursor = mSQLiteDB.rawQuery(query, null);
                mNodeCursor = mMotifDatabaseHelper.query(nodeTableName, null, null,
                        null, "RANDOM()", null, null);
            }

            if (mNodeCursor.moveToFirst()) {
                DotNode node;

                //============================================
                // LOAD "mCount" nodes
                //============================================
                int i = 0;
                do {

                    node = new DotNode(
                            mNodeCursor.getInt(MotifDatabaseHelper.KEY_ID),
                            mNodeCursor.getString(MotifDatabaseHelper.KEY_LABEL),
                            mNodeCursor.getInt(MotifDatabaseHelper.KEY_DEGREE));

                    if(mLoaderListener != null && mLoaderListener.isNodeValid(node.degree)){
                        dotNodes.add(node);
                        i++;
                    }

                    if(i >= mCount) break;

                } while (mNodeCursor.moveToNext());

                publishProgress(dotNodes);

                //============================================
                // LOAD REMAINING "mBufferCount" nodes
                //============================================
                int j = 0;
                do{
                    if(++j > mBufferCount ) break;

                    node = new DotNode(
                            mNodeCursor.getInt(MotifDatabaseHelper.KEY_ID),
                            mNodeCursor.getString(MotifDatabaseHelper.KEY_LABEL),
                            mNodeCursor.getInt(MotifDatabaseHelper.KEY_DEGREE));

                    bufferNodes.add(node);
                }while (mNodeCursor.moveToNext());
            }
            return bufferNodes;
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