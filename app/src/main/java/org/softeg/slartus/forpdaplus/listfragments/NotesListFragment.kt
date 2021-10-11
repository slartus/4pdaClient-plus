package org.softeg.slartus.forpdaplus.listfragments

import android.os.Bundle
import android.view.View
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.core.ui.dialogs.MenuItemAction
import org.softeg.slartus.forpdaplus.feature_notes.ui.NotesListFragment

class NotesListFragment : BaseBrickFragment(R.layout.fragment_notes_list_container) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        removeArrow()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.beginTransaction()
            .add(R.id.fragment_container, NotesListFragment.newInstance())
            .commitAllowingStateLoss()
    }

    override fun onResume() {
        super.onResume()
        removeArrow()
    }
//
//    override fun onCreateContextMenu(
//        menu: ContextMenu,
//        v: View,
//        menuInfo: ContextMenu.ContextMenuInfo?
//    ) {
//        val info = menuInfo as AdapterView.AdapterContextMenuInfo
//        if (info.id == -1L) return
//        if (info.id < 0 || adapter!!.count <= info.id) return
//        val o = adapter!!.getItem(info.id.toInt()) ?: return
//        val note = o as Note
//        val list: MutableList<MenuItemAction> = ArrayList()
//        addLinksSubMenu(list, note)
//        list.add(
//            MenuItemAction(
//                App.getContext().getString(R.string.delete)
//            ) {
//                MaterialDialog.Builder(requireContext())
//                    .title(R.string.confirm_action)
//                    .content(R.string.ask_delete_note)
//                    .cancelable(true)
//                    .negativeText(R.string.cancel)
//                    .positiveText(R.string.delete)
//                    .onPositive { _: MaterialDialog?, _: DialogAction? ->
//                        try {
//                            viewModel.delete(note.id.toString())
//                        } catch (ex: Throwable) {
//                            AppLog.e(context, ex)
//                        }
//                    }
//                    .show()
//            })
//        ExtUrl.showContextDialog(context, null, list)
//    }

//    private fun addLinksSubMenu(list: MutableList<MenuListDialog>, note: Note) {
//        try {
//            val links = note.links
//            if (links.size != 0) {
//                list.add(MenuListDialog(App.getContext().getString(R.string.links)) {
//                    val list1: MutableList<MenuListDialog> = ArrayList()
//                    for (pair in links) {
//                        list1.add(MenuListDialog(pair.first.toString()) {
//                            IntentActivity.tryShowUrl(
//                                context as Activity?,
//                                mHandler,
//                                pair.second.toString(),
//                                true,
//                                false,
//                                null
//                            )
//                        })
//                    }
//                    ExtUrl.showContextDialog(context, null, list1)
//                })
//            }
//
//        } catch (e: Throwable) {
//            AppLog.e(context, e)
//        }
//    }

    companion object {
        const val TOPIC_ID_KEY = "TOPIC_ID_KEY"
    }
}