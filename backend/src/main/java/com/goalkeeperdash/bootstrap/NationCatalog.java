package com.goalkeeperdash.bootstrap;

import java.util.List;

/**
 * Canonical nation seed list (§10): ISO 3166-1 alpha-3 codes, display names, and
 * two flag colors (CSV hex) for client rendering. Includes a 48-nation,
 * 2026-finalist-tier spread; Morocco (MAR) is present.
 */
final class NationCatalog {

    record Entry(String code, String name, String flagColors) {}

    private NationCatalog() {}

    static final List<Entry> NATIONS = List.of(
            new Entry("MAR", "Morocco", "#C1272D,#006233"),
            new Entry("FRA", "France", "#0055A4,#EF4135"),
            new Entry("BRA", "Brazil", "#009C3B,#FFDF00"),
            new Entry("ARG", "Argentina", "#74ACDF,#FFFFFF"),
            new Entry("ESP", "Spain", "#AA151B,#F1BF00"),
            new Entry("POR", "Portugal", "#006600,#FF0000"),
            new Entry("ENG", "England", "#FFFFFF,#CF142B"),
            new Entry("GER", "Germany", "#000000,#DD0000"),
            new Entry("NED", "Netherlands", "#AE1C28,#21468B"),
            new Entry("ITA", "Italy", "#008C45,#CD212A"),
            new Entry("BEL", "Belgium", "#000000,#FFD90C"),
            new Entry("CRO", "Croatia", "#FF0000,#171796"),
            new Entry("URU", "Uruguay", "#0038A8,#FFFFFF"),
            new Entry("COL", "Colombia", "#FCD116,#003893"),
            new Entry("MEX", "Mexico", "#006847,#CE1126"),
            new Entry("USA", "United States", "#3C3B6E,#B22234"),
            new Entry("JPN", "Japan", "#FFFFFF,#BC002D"),
            new Entry("KOR", "South Korea", "#FFFFFF,#CD2E3A"),
            new Entry("SEN", "Senegal", "#00853F,#FDEF42"),
            new Entry("SUI", "Switzerland", "#FF0000,#FFFFFF"),
            new Entry("DEN", "Denmark", "#C60C30,#FFFFFF"),
            new Entry("SRB", "Serbia", "#C6363C,#0C4076"),
            new Entry("POL", "Poland", "#FFFFFF,#DC143C"),
            new Entry("WAL", "Wales", "#C8102E,#00B140"),
            new Entry("AUS", "Australia", "#00843D,#FFCD00"),
            new Entry("CAN", "Canada", "#FF0000,#FFFFFF"),
            new Entry("ECU", "Ecuador", "#FFDD00,#034EA2"),
            new Entry("GHA", "Ghana", "#006B3F,#CE1126"),
            new Entry("CMR", "Cameroon", "#007A5E,#CE1126"),
            new Entry("TUN", "Tunisia", "#E70013,#FFFFFF"),
            new Entry("CRC", "Costa Rica", "#002B7F,#CE1126"),
            new Entry("NGA", "Nigeria", "#008751,#FFFFFF"),
            new Entry("EGY", "Egypt", "#CE1126,#000000"),
            new Entry("ALG", "Algeria", "#006233,#FFFFFF"),
            new Entry("CIV", "Ivory Coast", "#F77F00,#009E60"),
            new Entry("QAT", "Qatar", "#8A1538,#FFFFFF"),
            new Entry("KSA", "Saudi Arabia", "#006C35,#FFFFFF"),
            new Entry("IRN", "Iran", "#239F40,#DA0000"),
            new Entry("PER", "Peru", "#D91023,#FFFFFF"),
            new Entry("CHI", "Chile", "#0039A6,#D52B1E"),
            new Entry("PAR", "Paraguay", "#D52B1E,#0038A8"),
            new Entry("SCO", "Scotland", "#005EB8,#FFFFFF"),
            new Entry("AUT", "Austria", "#ED2939,#FFFFFF"),
            new Entry("SWE", "Sweden", "#006AA7,#FECC00"),
            new Entry("NOR", "Norway", "#BA0C2F,#00205B"),
            new Entry("UKR", "Ukraine", "#0057B7,#FFD700"),
            new Entry("TUR", "Turkey", "#E30A17,#FFFFFF"),
            new Entry("RSA", "South Africa", "#007A4D,#FFB612"));

    static List<Entry> nations() {
        return NATIONS;
    }
}
