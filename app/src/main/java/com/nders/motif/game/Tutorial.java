package com.nders.motif.game;

import android.content.Context;
import android.util.Log;
import android.util.SparseIntArray;

import com.nders.motif.entities.DotColor;
import com.nders.motif.entities.DotNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Tutorial {

    private static final String TAG = Tutorial.class.getSimpleName();
    public enum MILESTONE {COLOR_CHECK, DOT_COUNT, COLOR_AND_COUNT_CHECK}

    private Queue<MILESTONE> mMilestones;
    private Queue<String> mInstructions;
    private Queue<Integer> mDotCounts;
    private Queue<DotColor> mDotColors;
    private Queue<Integer> mDotColorCount;
    private Map<List, Boolean> mEdges;
    private List<DotNode> mDotNodes;
    private Queue<String> mIdleInstructions;
    private boolean mUpdateInstruction = false;
    private Queue<List> mDotsDimensions;

    private static final List<List<DotNode>> sAllNodes = new ArrayList<>();
    private static final List<Map<List, Boolean>> sAllEdges = new ArrayList<>();

    private Tutorial(){
        mMilestones = new LinkedList<>();
        mDotColors = new LinkedList<>();
        mDotColorCount = new LinkedList<>();
        mDotCounts = new LinkedList<>();
        mInstructions = new LinkedList<>();
        mIdleInstructions = new LinkedList<>();
        mDotsDimensions = new LinkedList<>();
    }

    public static Queue<Tutorial> tutorialList(Context context){

        readJSONFile(context);

        Queue<Tutorial> tutorials = new LinkedList<>();

        Tutorial t1 = new Tutorial();
        t1.mMilestones.add(MILESTONE.DOT_COUNT);
        t1.mDotCounts.add(2);
        t1.mDotsDimensions.add(Arrays.asList(1, 2));
        t1.mEdges = sAllEdges.get(0);
        t1.mDotNodes = sAllNodes.get(0);
        t1.mIdleInstructions.add("Connect the two dots to continue.");
        tutorials.add(t1);

        Tutorial t2 = new Tutorial();
        t2.mMilestones.add(MILESTONE.DOT_COUNT);
        t2.mDotCounts.add(2);
        t2.mDotsDimensions.add(Arrays.asList(2, 1));
        t2.mEdges = sAllEdges.get(0);
        t2.mDotNodes = sAllNodes.get(0);
        t2.mInstructions.add("Connections can also be vertical...");
        tutorials.add(t2);

        Tutorial t3 = new Tutorial();
        t3.mMilestones.add(MILESTONE.DOT_COUNT);
        t3.mMilestones.add(MILESTONE.DOT_COUNT);
        t3.mDotCounts.addAll(Arrays.asList(2, 2));
        t3.mDotsDimensions.add(Arrays.asList(2,3));
        t3.mInstructions.add("...but NOT diagonal.");
        t3.mInstructions.add("...but NOT diagonal.");
        t3.mEdges = sAllEdges.get(1);
        t3.mDotNodes = sAllNodes.get(1);
        tutorials.add(t3);

        Tutorial t4 = new Tutorial();

        t4.mMilestones.add(MILESTONE.COLOR_CHECK);
        t4.mDotColors.add(DotColor.RED);
        t4.mDotColorCount.add(14);
        t4.mInstructions.add("TOUCHING a dot, shows\n\nother dots it can\n\nconnect to in the same color.");
        t4.mIdleInstructions.add("Find the RED dot with\n\nthe most connections.");

        t4.mMilestones.add(MILESTONE.DOT_COUNT);
        t4.mDotCounts.add(12);
        t4.mInstructions.add("Now connect as many\n\ndots as possible.");
        t4.mIdleInstructions.add("Connect the RED dot\n\nwith most connections");

        t4.mDotsDimensions.add(Arrays.asList(4,4));
        t4.mEdges = sAllEdges.get(2);
        t4.mDotNodes = sAllNodes.get(2);
        tutorials.add(t4);

        return tutorials;
    }


    public List dimensions(){
        return mDotsDimensions.peek();
    }
    public final MILESTONE milestone() {
        return mMilestones.peek();
    }

    public final String instruction() {
        if(mUpdateInstruction){
            if(!mInstructions.isEmpty()){
                mInstructions.poll();
            }
            if(!mIdleInstructions.isEmpty()){
                mIdleInstructions.poll();
            }
            mUpdateInstruction = false;
        }
        return mInstructions.isEmpty()? null: mInstructions.peek();
    }

    public final String idleInstructions(){
        return mIdleInstructions.isEmpty()? null: mIdleInstructions.peek();
    }

    public final boolean milestoneReached(DotColor dotColor, int dotCount, int startid) {
        boolean result = false;
        switch (mMilestones.peek()){
            case DOT_COUNT:
                if(dotCount >= mDotCounts.peek()){
                    mDotCounts.poll();
                    result = true;
                }
                break;
            case COLOR_CHECK:
                if(dotColor == mDotColors.peek() && countRelatedDots(startid) == mDotColorCount.peek()){
                    mDotColors.poll();
                    mDotColorCount.poll();
                    result = true;
                }
                break;
            case COLOR_AND_COUNT_CHECK:
                if(dotCount >= mDotCounts.peek() && dotColor == mDotColors.peek() && countRelatedDots(startid) == mDotColorCount.peek()){
                    mDotCounts.poll();
                    mDotColors.poll();
                    mDotColorCount.poll();
                    result = true;
                }
                break;
        }
        if(result){
            mMilestones.poll();
            if(!mInstructions.isEmpty()){
                mUpdateInstruction = true;
            }
        }

        return result;
    }

    public boolean complete() {
        return mMilestones.isEmpty();
    }

    public boolean checkEdge(int idA, int idB){
        int min = idA < idB? idA: idB;
        int max = idA >= idB? idA: idB;
        List<Integer> key = Arrays.asList(min, max);
        return mEdges.containsKey(key) && (Boolean)mEdges.get(key);
    }

    public List<DotNode> data(){
        return mDotNodes;
    }

    private int countRelatedDots(int startId){
        int count = 0;
        for(DotNode node: mDotNodes){
            if(startId != node.id && checkEdge(startId, node.id)){
                count++;
            }
        }
        return count;
    }

    private static void readJSONFile(Context context){
        String filename = "tutorial_data.json";

        try{
            InputStream in = context.getAssets().open(filename);
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();
            String jsonString = new String(buffer, "UTF-8");

            JSONArray array = new JSONArray(jsonString);
            for(int i = 0, len = array.length(); i < len; i++){
                JSONObject obj = array.getJSONObject(i);

                // Nodes
                JSONArray jsonNodes = obj.getJSONArray("nodes");
                Log.i(TAG, jsonNodes.toString());
                List<DotNode> nodes = new ArrayList<>();

                for(int j = 0, count=jsonNodes.length(); j < count; j++){
                    JSONObject node = jsonNodes.getJSONObject(j);
                    Log.i(TAG, node.toString());
                    nodes.add(new DotNode(node.getInt("id"), "", node.getInt("degree")));
                }
                sAllNodes.add(nodes);
                Log.i(TAG, "******************Parsed nodes successfully");

                // Edges
                JSONArray jsonEdges = obj.getJSONArray("edges");
                Map<List, Boolean> edges = new HashMap<>();

                for(int j = 0, count=jsonEdges.length(); j < count; j++){
                    JSONArray edge = jsonEdges.getJSONArray(j);
                    for(int k = 0, kk=edge.length(); k < kk; k++){
                        List<Integer> key = new ArrayList<>();
                        key.add(nodes.get(j).id);
                        key.add((Integer)edge.get(k));
                        edges.put(key, true);
                    }
                }
                sAllEdges.add(edges);
            }
        }catch (JSONException e){
            Log.e(TAG, "ERROR READING JSON\n" + e.getMessage());
        }catch (IOException e){
            Log.e(TAG, "IO ERROR\n" + e.getMessage());
        }

    }
}
