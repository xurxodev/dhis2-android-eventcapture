/*
 * Copyright (c) 2015, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis2.android.eventcapture.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.eventcapture.adapters.SimpleAdapter;
import org.hisp.dhis2.android.sdk.controllers.Dhis2;
import org.hisp.dhis2.android.sdk.persistence.models.OrganisationUnit$Table;
import org.hisp.dhis2.android.sdk.persistence.models.Program;

import java.util.List;

public class ProgramDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {
    private static String TAG = ProgramDialogFragment.class.getName();

    private ListView mListView;
    private SimpleAdapter<Program> mAdapter;
    private OnDatasetSetListener mListener;

    public static ProgramDialogFragment newInstance(OnDatasetSetListener listener,
                                                    String orgUnitId) {
        ProgramDialogFragment fragment = new ProgramDialogFragment();
        Bundle args = new Bundle();
        args.putString(OrganisationUnit$Table.ID, orgUnitId);
        fragment.setOnClickListener(listener);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE,
                R.style.Theme_AppCompat_Light_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment_listview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mListView = (ListView) view.findViewById(R.id.simple_listview);
        mListView.setOnItemClickListener(this);

        mAdapter = new SimpleAdapter<>(getActivity());
        mAdapter.setStringExtractor(new StringExtractor());
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        List<Program> programs = Dhis2.getInstance()
                .getMetaDataController()
                .getProgramsForOrganisationUnit(
                        getArguments().getString(OrganisationUnit$Table.ID),
                        Program.SINGLE_EVENT_WITHOUT_REGISTRATION
                );
        mAdapter.swapData(programs);
    }

    public void setOnClickListener(OnDatasetSetListener listener) {
        mListener = listener;
    }

    public void show(FragmentManager manager) {
        show(manager, TAG);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null) {
            Program program = mAdapter.getItemSafely(position);
            if (program != null) {
                mListener.onDataSetSelected(
                        program.getId(), program.getName()
                );
            }
        }
        dismiss();
    }

    public interface OnDatasetSetListener {
        public void onDataSetSelected(String dataSetId, String dataSetName);
    }

    static class StringExtractor implements SimpleAdapter.ExtractStringCallback<Program> {

        @Override
        public String getString(Program object) {
            return object.getName();
        }
    }
}
