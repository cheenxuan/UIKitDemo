package com.baidu.ocr.sdk.model;


import java.util.List;

/**
 * @auther: xuan
 * @date : 2023/11/1 .
 * <p>
 * Description:
 * <p>
 */
public class Word extends WordSimple {
    private Location location = new Location();
    private List<VertexesLocation> vertexesLocation;
    private List<Char> characterResults;

    public Word() {
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<VertexesLocation> getVertexesLocations() {
        return this.vertexesLocation;
    }

    public void setVertexesLocations(List<VertexesLocation> vertexesLocation) {
        this.vertexesLocation = vertexesLocation;
    }

    public List<Char> getCharacterResults() {
        return this.characterResults;
    }

    public void setCharacterResults(List<Char> characterResults) {
        this.characterResults = characterResults;
    }

    public static class Char {
        private Location location = new Location();
        private String character;

        public Char() {
        }

        public String getCharacter() {
            return this.character;
        }

        public void setCharacter(String character) {
            this.character = character;
        }

        public Location getLocation() {
            return this.location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }
    }
}