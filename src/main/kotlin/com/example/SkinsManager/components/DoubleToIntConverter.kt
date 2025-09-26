package com.example.SkinsManager.components

import com.vaadin.flow.data.binder.Result
import com.vaadin.flow.data.binder.ValueContext
import com.vaadin.flow.data.converter.Converter

class DoubleToIntConverter : Converter<Double?, Int?> {
    override fun convertToModel(value: Double?, context: ValueContext): Result<Int?> {
        return Result.ok(value?.toInt())
    }

    override fun convertToPresentation(value: Int?, context: ValueContext): Double? {
        return value?.toDouble()
    }
}
