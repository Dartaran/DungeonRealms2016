package net.dungeonrealms.entities.types.pets;

/**
 * Created by Kieran on 10/15/2015.
 */
public enum En*umPets {
    WOLF(0, "WOLF", 95),
    ENDERMITE(1, "ENDERMITE", 67),
    SILVERFISH(2, "SILVERFISH", 60),
    CAVE_SPIDER(3, "CAVESPIDER", 59),
    BABY_ZOMBIE(4, "BABYZOMBIE", 54),
    BABY_PIGZOMBIE(5, "BABYPIGZOMBIE", 57),
    SNOWMAN(6, "SNOWMAN", 56),
    OCELOT(7, "OCELOT", 98),
    RABBIT(8, "RABBIT", 101),
    CHICKEN(9, "CHICKEN", 93);

    private int id;
    private String name;
    private int eggShortData;

    public int getId() {
        return id;
    }

    public String getRawName() {
        return name;
    }

    public int getEggShortData() {
        return eggShortData;
    }

    EnumPets(int id, String name, int eggShortData) {
        this.id = id;
        this.name = name;
        this.eggShortData = eggShortData;
    }

    public static EnumPets getById(int id) {
        for (EnumPets ep : values()) {
            if (ep.getId() == id) {
                return ep;
            }
        }
        return null;
    }

    public static EnumPets getByName(String rawName) {
        for (EnumPets ep : values()) {
            if (ep.getRawName().equalsIgnoreCase(rawName)) {
                return ep;
            }
        }
        return null;
    }
}