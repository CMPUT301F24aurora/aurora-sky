package com.example.lotteryapp;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.UiController;
import org.hamcrest.Matcher;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static org.hamcrest.Matchers.allOf;

public class CustomScrollActions {

    public static ViewAction scrollToHolder(final Matcher<View> viewHolderMatcher) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(RecyclerView.class);
            }

            @Override
            public String getDescription() {
                return "scroll RecyclerView to the view holder";
            }

            @Override
            public void perform(UiController uiController, View view) {
                RecyclerView recyclerView = (RecyclerView) view;
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View child = recyclerView.getChildAt(i);
                    RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(child);
                    if (viewHolderMatcher.matches(holder)) {
                        child.performClick();
                        return;
                    }
                }
            }
        };
    }
}
