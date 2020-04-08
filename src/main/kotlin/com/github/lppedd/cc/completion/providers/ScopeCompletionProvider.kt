@file:Suppress("DEPRECATION")

package com.github.lppedd.cc.completion.providers

import com.github.lppedd.cc.api.CommitScopeProvider
import com.github.lppedd.cc.api.ProviderPresentation
import com.github.lppedd.cc.api.SCOPE_EP
import com.github.lppedd.cc.completion.resultset.ResultSet
import com.github.lppedd.cc.configuration.CCConfigService
import com.github.lppedd.cc.lookupElement.CommitScopeLookupElement
import com.github.lppedd.cc.parser.CommitContext.ScopeCommitContext
import com.github.lppedd.cc.psiElement.CommitScopePsiElement
import com.github.lppedd.cc.runWithCheckCanceled
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal class ScopeCompletionProvider(
    private val project: Project,
    private val context: ScopeCommitContext,
) : CommitCompletionProvider<CommitScopeProvider> {
  override val providers: List<CommitScopeProvider> = SCOPE_EP.getExtensions(project)
  override val stopHere = false

  override fun complete(resultSet: ResultSet) {
    val rs = resultSet.withPrefixMatcher(context.scope.trim())
    providers.asSequence()
      .flatMap { provider ->
        runWithCheckCanceled {
          val wrapper = ScopeProviderWrapper(provider)
          provider.getCommitScopes(context.type)
            .asSequence()
            .take(200)
            .map { wrapper to it }
        }
      }
      .map { it.first to CommitScopePsiElement(project, it.second) }
      .mapIndexed { i, (provider, psi) -> CommitScopeLookupElement(i, provider, psi) }
      .distinctBy(CommitScopeLookupElement::getLookupString)
      .forEach(rs::addElement)
  }
}

internal class ScopeProviderWrapper(private val provider: CommitScopeProvider) : ProviderWrapper {
  override fun getId(): String =
    provider.getId()

  override fun getPresentation(): ProviderPresentation =
    provider.getPresentation()

  override fun getPriority(project: Project) =
    Priority(CCConfigService.getInstance(project).getProviderOrder(provider))
}
