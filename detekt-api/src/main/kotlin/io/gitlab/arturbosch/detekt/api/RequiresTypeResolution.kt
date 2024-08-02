package io.gitlab.arturbosch.detekt.api

import org.jetbrains.kotlin.resolve.BindingContext

interface RequiresTypeResolution {
    var bindingContext: BindingContext
}
