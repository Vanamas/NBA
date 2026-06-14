package cz.vanama.courtflow.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class ArchitectureTest {
    @Test
    fun `view models should reside in a per-screen feature package`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue { it.resideInPackage("..feature..") }
    }

    @Test
    fun `use cases should reside in a usecase package`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("UseCase")
            .assertTrue { it.resideInPackage("..domain.usecase..") }
    }

    @Test
    fun `domain stays free of android and ui dependencies`() {
        Konsist
            .scopeFromModule("domain")
            .files
            .assertFalse { file ->
                file.imports.any { import ->
                    (import.name.startsWith("android.") || import.name.startsWith("androidx.")) &&
                        !import.name.startsWith("androidx.paging.") &&
                        !import.name.startsWith("androidx.annotation.")
                }
            }
    }

    @Test
    fun `feature modules depend on domain interfaces, not data or network`() {
        Konsist
            .scopeFromModule("feature/players", "feature/teams")
            .files
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith("cz.vanama.courtflow.data.") ||
                        import.name.startsWith("cz.vanama.courtflow.core.network.")
                }
            }
    }

    @Test
    fun `repository implementations reside in the data repository package`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("RepositoryImpl")
            .assertTrue { it.resideInPackage("..data.repository..") }
    }

    @Test
    fun `repository interfaces reside in the domain repository package`() {
        // Guards against the "RepositoryImpl"-suffix dodge: a repository abstraction must
        // be a domain interface, not a class living elsewhere (e.g. a settings store named
        // "...Repository" hiding in core:common). Local preference stores are named *Store.
        Konsist
            .scopeFromProject()
            .interfaces()
            .withNameEndingWith("Repository")
            .assertTrue { it.resideInPackage("..domain.repository..") }
    }
}
