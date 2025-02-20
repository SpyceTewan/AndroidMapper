package at.tewan.androidmapper.beatmap.enums;

/**
 * @author Stefan Heinz
 * Enumeration of all difficulties.
 * Enum values include difficulty rank id
 */
public enum Difficulties {
    Easy(1),
    Normal(3),
    Hard(5),
    Expert(7),
    ExpertPlus(9);

    private int rank;

    Difficulties(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

}
