package org.softeg.slartus.forpdaplus.listfragments.next;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.Topic;
import org.softeg.slartus.forpdacommon.ActionSelectDialogFragment;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.listfragments.TopicUtils;
import org.softeg.slartus.forpdaplus.post.EditPostActivity;

/*
 * Created by slinkin on 27.02.2015.
 */
public abstract class TopicsFragment extends TopicsFragmentListBase {

    @Override
    protected void onTopicClick(final Topic topic){
        try {
            if (TextUtils.isEmpty(topic.getId())) return;
            if (tryCreatePost(topic))
                return;
            ActionSelectDialogFragment.execute(getActivity(),
                    "Действие по умолчанию",
                    String.format("%s.navigate_action", getListName()),
                    new CharSequence[]{getString(R.string.navigate_getfirstpost), getString(R.string.navigate_getlastpost),
                            getString(R.string.navigate_getnewpost), getString(R.string.navigate_last_url)},
                    new CharSequence[]{Topic.NAVIGATE_VIEW_FIRST_POST, Topic.NAVIGATE_VIEW_LAST_POST,
                            Topic.NAVIGATE_VIEW_NEW_POST, Topic.NAVIGATE_VIEW_LAST_URL},
                    new ActionSelectDialogFragment.OkListener() {
                        @Override
                        public void execute(CharSequence value) {
                            showTopicActivity(topic, TopicUtils.getUrlArgs(topic.getId(), value.toString(),
                                    Topic.NAVIGATE_VIEW_FIRST_POST.toString()));
                        }
                    }, "Вы можете изменить действие по умолчанию долгим тапом по теме"
            );


        } catch (Throwable ex) {
            AppLog.e(getActivity(), ex);
        }
    }

    /**
     * Извне создание поста
     */
    public Boolean tryCreatePost(Topic topic) {
        Bundle extras = getArguments();
        if (extras == null)
            return false;
        if (!extras.containsKey(Intent.EXTRA_STREAM) &&
                !extras.containsKey(Intent.EXTRA_TEXT) &&
                !extras.containsKey(Intent.EXTRA_HTML_TEXT)) return false;

        EditPostActivity.newPostWithAttach(getActivity(),
                null, topic.getId(), Client.getInstance().getAuthKey(), extras);
        getActivity().finish();
        return true;
    }

    private void showTopicActivity(Topic topic, String args) {
        ExtTopic.showActivity(getActivity(), topic.getId(), args);
        topicAfterClick(topic);

}

    private void topicAfterClick(Topic topic) {
        topic.setState(IListItem.STATE_NORMAL);
      //  getAdapter().notifyDataSetChanged();

        updateCache();
    }



}
