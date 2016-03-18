package com.peoplepinpoint.peoplepinpoint.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.peoplepinpoint.peoplepinpoint.R;
import com.peoplepinpoint.peoplepinpoint.entity.PersonEntity;

/**
 * @author Manoj Khanna
 */

public class AccountFragment extends Fragment {

    public static AccountFragment newInstance() {
        return new AccountFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        PersonEntity localPersonEntity = ((MainActivity) getActivity()).getLocalPersonEntity();
        String name = localPersonEntity.getName();

        TextView pictureTextView = (TextView) view.findViewById(R.id.picture_text_view_account);
        pictureTextView.setText(String.valueOf(name.charAt(0)).toUpperCase());

        TextView nameTextView = (TextView) view.findViewById(R.id.name_text_view_account);
        nameTextView.setText(name);

        TextView emailTextView = (TextView) view.findViewById(R.id.email_text_view_account);
        emailTextView.setText(localPersonEntity.getEmail());

        return view;
    }

}
