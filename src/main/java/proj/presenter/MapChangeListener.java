package proj.presenter;

import proj.model.maps.AbstractWorldMap;

public interface MapChangeListener {
    void mapChanged(AbstractWorldMap worldMap, String message);
}
