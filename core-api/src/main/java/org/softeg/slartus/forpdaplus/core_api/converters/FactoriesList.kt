package org.softeg.slartus.forpdaplus.core_api.converters

import retrofit2.Converter

class FactoriesList(val list: List<Converter.Factory>) : List<Converter.Factory> by list