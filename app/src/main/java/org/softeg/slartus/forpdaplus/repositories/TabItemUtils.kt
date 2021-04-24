package org.softeg.slartus.forpdaplus.repositories

import org.softeg.slartus.forpdaplus.tabs.TabItem

fun TabItem.setTabTitle(title: String) = TabsRepository.instance.setTabTitle(this, title)
fun TabItem.setTabUrl(url: String) = TabsRepository.instance.setTabUrl(this, url)
fun TabItem.setTabTag(tag: String) = TabsRepository.instance.setParentTag(this, tag)