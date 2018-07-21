package com.ghstudios.android.features.quests;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ghstudios.android.AppSettings;
import com.ghstudios.android.components.ColumnLabelTextCell;
import com.ghstudios.android.components.TitleBarCell;
import com.ghstudios.android.data.classes.Location;
import com.ghstudios.android.data.classes.Quest;
import com.ghstudios.android.data.database.DataManager;
import com.ghstudios.android.mhgendatabase.R;
import com.ghstudios.android.features.locations.LocationDetailPagerActivity;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ghstudios.android.mhgendatabase.R.id.location;

public class QuestDetailFragment extends Fragment {
    private static final String ARG_QUEST_ID = "QUEST_ID";
    
    private Quest mQuest;
    private View mView;

    @BindView(R.id.location_layout) LinearLayout mQuestLocationLayout;
    @BindView(R.id.location_image) ImageView questLocationImageView;

    @BindView(R.id.titlebar) TitleBarCell titleBarCell;
    @BindView(R.id.level) TextView levelTextView;
    @BindView(R.id.hub) TextView hubTextView;
    @BindView(R.id.reward) ColumnLabelTextCell rewardCell;
    @BindView(R.id.hrp) ColumnLabelTextCell hrpCell;
    @BindView(R.id.fee) ColumnLabelTextCell feeCell;

    @BindView(R.id.goal) TextView goalTextView;
    @BindView(R.id.location) TextView locationTextView;
    @BindView(R.id.subquest) TextView subquestTextView;
    @BindView(R.id.subhrp) TextView subquestHrpTextView;
    @BindView(R.id.subreward) TextView subRewardTextView;
    @BindView(R.id.description) TextView mFlavor;

    public static QuestDetailFragment newInstance(long questId) {
        Bundle args = new Bundle();
        args.putLong(ARG_QUEST_ID, questId);
        QuestDetailFragment f = new QuestDetailFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        QuestDetailViewModel viewModel = ViewModelProviders.of(getActivity()).get(QuestDetailViewModel.class);

        viewModel.getQuest().observe(this, quest -> {
            mQuest = quest;
            updateUI();
        });

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quest_detail, container, false);

        ButterKnife.bind(this, view);

        // Click listener for quest location
        mQuestLocationLayout.setOnClickListener(v -> {
            // The id argument will be the Monster ID; CursorAdapter gives us this
            // for free
            Intent i = new Intent(getActivity(), LocationDetailPagerActivity.class);
            long id = (long)v.getTag();
            if(id>100) id = id-100;
            i.putExtra(LocationDetailPagerActivity.EXTRA_LOCATION_ID, id);
            startActivity(i);
        });

        return view;
    }

    
    private void updateUI() {
        // Add list of monsters and habitats
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().add(
                R.id.monster_habitat_fragment,
                QuestMonsterFragment.newInstance(mQuest.getId())
        ).commitAllowingStateLoss();

        String cellGoal = mQuest.getGoal();
        String cellHrp = "" + mQuest.getHrp();
        String cellReward = "" + mQuest.getReward() + "z";
        String cellFee = "" + mQuest.getFee() + "z";
        String cellLocation = mQuest.getLocation().getName();
        String cellSubGoal = mQuest.getSubGoal();
        String cellSubHrp = "" + mQuest.getSubHrp();
        String cellSubReward = "" + mQuest.getSubReward() + "z";
        String flavor = mQuest.getFlavor();

        // bind title bar
        titleBarCell.setIconResource(getIconForQuest(mQuest));
        titleBarCell.setTitleText(mQuest.getName());
        titleBarCell.setAltTitleText(mQuest.getJpnName());
        titleBarCell.setAltTitleEnabled(AppSettings.isJapaneseEnabled());

        // bind details section
        hubTextView.setText(mQuest.getHub());
        levelTextView.setText(String.valueOf(mQuest.getStars()));
        hrpCell.setValueText(cellHrp);
        rewardCell.setValueText(cellReward);
        feeCell.setValueText(cellFee);

        goalTextView.setText(cellGoal);
        locationTextView.setText(cellLocation);
        locationTextView.setTag(mQuest.getLocation().getId());
        subquestTextView.setText(cellSubGoal);
        subquestHrpTextView.setText(cellSubHrp);
        subRewardTextView.setText(cellSubReward);
        mQuestLocationLayout.setTag(mQuest.getLocation().getId());
        mFlavor.setText(flavor);

        // Get Location based on ID and set image thumbnail
        DataManager dm = DataManager.get(getContext());
        Location loc = dm.getLocation(mQuest.getLocation().getId());
        String cellImage = "icons_location/" + loc.getFileLocationMini();

        questLocationImageView.setTag(mQuest.getLocation().getId());
        new LoadImage(questLocationImageView, cellImage, getContext()).execute();
    }

    /**
     * TODO: Needs to be defined in a better way that avoids repetition,
     * but this is the easiest way for now.
     * @param quest
     * @return
     */
    private int getIconForQuest(Quest quest) {
        if (quest.getHunterType() == 1) {
            return R.drawable.quest_cat;
        }

        switch (quest.getGoalType()) {
            case Quest.QUEST_GOAL_DELIVER:
                return R.drawable.quest_icon_green;
            case Quest.QUEST_GOAL_CAPTURE:
                return R.drawable.quest_icon_grey;
            default:
                return R.drawable.quest_icon_red;
        }
    }

    protected class LoadImage extends AsyncTask<Void,Void,Drawable> {
        private ImageView mImage;
        private String path;
        private String imagePath;
        private Context context;

        public LoadImage(ImageView imv, String imagePath, Context c) {
            this.mImage = imv;
            this.path = imv.getTag().toString();
            this.imagePath = imagePath;
            this.context = c;
        }

        @Override
        protected Drawable doInBackground(Void... arg0) {
            Drawable d = null;

            try {
                d = Drawable.createFromStream(context.getAssets().open(imagePath),
                        null);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return d;
        }

        protected void onPostExecute(Drawable result) {
            if (mImage.getTag().toString().equals(path)) {
                mImage.setImageDrawable(result);
            }
        }
    }
}
