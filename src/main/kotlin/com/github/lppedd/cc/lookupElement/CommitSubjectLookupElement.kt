package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.completion.providers.SubjectProviderWrapper
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitSubjectPsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
 * @author Edoardo Luppi
 */
internal class CommitSubjectLookupElement(
    index: Int,
    provider: SubjectProviderWrapper,
    private val psiElement: CommitSubjectPsiElement,
) : CommitLookupElement(index, CC.Tokens.PrioritySubject, provider) {
  private val commitSubject = psiElement.commitSubject

  override fun getPsiElement(): CommitSubjectPsiElement =
    psiElement

  override fun getLookupString(): String =
    commitSubject.text

  override fun renderElement(presentation: LookupElementPresentation) =
    presentation.let {
      it.icon = CCIcons.Tokens.Subject
      it.itemText = lookupString
      it.isTypeIconRightAligned = true

      val rendering = commitSubject.getRendering()
      it.isItemTextBold = rendering.bold
      it.isItemTextItalic = rendering.italic
      it.isStrikeout = rendering.strikeout
      it.setTypeText(rendering.type, rendering.icon)
    }

  override fun handleInsert(context: InsertionContext) {
    val editor = context.editor
    val document = context.document

    val (lineStart, lineEnd) = editor.getCurrentLineRange()
    val line = document.getSegment(lineStart, lineEnd)
    val subject = CCParser.parseHeader(line).subject
    val subjectStartOffset = lineStart + ((subject as? ValidToken)?.range?.startOffset ?: 0)
    val elementValue = commitSubject.getValue(context.toTokenContext())
    val subjectText = " $elementValue"

    document.replaceString(subjectStartOffset, lineEnd, subjectText)
    editor.moveCaretToOffset(subjectStartOffset + subjectText.length)
  }
}
