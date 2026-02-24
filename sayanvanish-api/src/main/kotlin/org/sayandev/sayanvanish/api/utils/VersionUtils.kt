package org.sayandev.sayanvanish.api.utils

object VersionUtils {
    fun compare(first: String, second: String): Int {
        val firstVersion = ParsedVersion.parse(first)
        val secondVersion = ParsedVersion.parse(second)

        if (firstVersion == null && secondVersion == null) {
            return first.trim().compareTo(second.trim(), ignoreCase = true)
        }
        if (firstVersion == null) return -1
        if (secondVersion == null) return 1
        return firstVersion.compareTo(secondVersion)
    }

    fun isNewer(candidate: String, current: String): Boolean {
        return compare(candidate, current) > 0
    }

    private data class ParsedVersion(
        val major: Int,
        val minor: Int,
        val patch: Int,
        val preRelease: List<Identifier>,
    ) : Comparable<ParsedVersion> {
        override fun compareTo(other: ParsedVersion): Int {
            if (major != other.major) return major.compareTo(other.major)
            if (minor != other.minor) return minor.compareTo(other.minor)
            if (patch != other.patch) return patch.compareTo(other.patch)

            if (preRelease.isEmpty() && other.preRelease.isEmpty()) return 0
            if (preRelease.isEmpty()) return 1
            if (other.preRelease.isEmpty()) return -1

            val max = maxOf(preRelease.size, other.preRelease.size)
            for (index in 0 until max) {
                val left = preRelease.getOrNull(index)
                val right = other.preRelease.getOrNull(index)
                if (left == null) return -1
                if (right == null) return 1
                val cmp = left.compareTo(right)
                if (cmp != 0) return cmp
            }
            return 0
        }

        companion object {
            private val trailingCommitPattern = Regex("^(.*)-(?:[0-9a-fA-F]{6,40})$")
            private val corePattern = Regex("^(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?")
            private val numberPattern = Regex("\\d+")

            fun parse(rawVersion: String): ParsedVersion? {
                var normalized = rawVersion.trim()
                if (normalized.isEmpty()) return null

                normalized = normalized.substringBefore('+')
                normalized = normalized.removePrefix("v").removePrefix("V")

                val commitMatch = trailingCommitPattern.matchEntire(normalized)
                if (commitMatch != null && commitMatch.groupValues[1].contains('.')) {
                    normalized = commitMatch.groupValues[1]
                }

                val split = normalized.split('-', limit = 2)
                val core = split[0]
                val preRaw = split.getOrNull(1)

                val coreMatch = corePattern.find(core) ?: return null
                val major = coreMatch.groupValues[1].toInt()
                val minor = coreMatch.groupValues[2].takeIf { it.isNotEmpty() }?.toInt() ?: 0
                val patch = coreMatch.groupValues[3].takeIf { it.isNotEmpty() }?.toInt() ?: 0

                val preIdentifiers =
                    preRaw
                        ?.replace('-', '.')
                        ?.replace('_', '.')
                        ?.split('.')
                        ?.filter { it.isNotBlank() }
                        ?.map { token ->
                            val numeric = token.toIntOrNull()
                            if (numeric != null) {
                                Identifier.Numeric(numeric)
                            } else {
                                val fromToken = numberPattern.find(token)?.value?.toIntOrNull()
                                if (fromToken != null && token.all { it.isDigit() }) {
                                    Identifier.Numeric(fromToken)
                                } else {
                                    Identifier.Text(token.lowercase())
                                }
                            }
                        }
                        ?: emptyList()

                return ParsedVersion(major, minor, patch, preIdentifiers)
            }
        }
    }

    private sealed class Identifier : Comparable<Identifier> {
        data class Numeric(val value: Int) : Identifier()

        data class Text(val value: String) : Identifier()

        override fun compareTo(other: Identifier): Int {
            return when (this) {
                is Numeric ->
                    when (other) {
                        is Numeric -> value.compareTo(other.value)
                        is Text -> -1
                    }

                is Text ->
                    when (other) {
                        is Numeric -> 1
                        is Text -> value.compareTo(other.value)
                    }
            }
        }
    }
}
