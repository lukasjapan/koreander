package de.cvguy.kotlin.koreander.exception

class TemplateNotFoundException(resourceLocation: String) : KoreanderException("Template '$resourceLocation' not found in resources.")