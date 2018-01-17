package ise.roletagger.model;

public class MapEntity {
	private final Entity entity;
	/**
	 * how many times this entity has been seen
	 */
	private long frequency = 1;

	public MapEntity(Entity entity) {
		this.entity = entity;
	}

	public void increment(){
		frequency++;
	}

	public long getFrequency() {
		return frequency;
	}

	public Entity getEntity() {
		return entity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		MapEntity other = (MapEntity) obj;
		if (entity.getUri().equals(other.getEntity().getUri())) {
			return true;
		}else{
			return false;
		}

	}

	@Override
	public String toString() {
		return "MapEntity [entity=" + entity + ", frequency=" + frequency + "]";
	}

}
