package ise.roletagger.model;

/**
 * This class models the input which is needed for creating dictionary.
 * The format should be:
 * Plain text label;URI;Name_of_the_entity
 * For example you can have a look at folder "Wikidata" or "entities"
 * @author fbm
 *
 */
public class Entity {
	private final String name;
	private final String uri;
	private final String entityName;
	/**
	 * Do not forget: file name (in the "entitieswikidata" folder) and category name should be same
	 */
	private final Category categoryFolder;

	public Entity(String name, String uri, String entityName, Category categoryFolder) {
		super();
		this.name = name;
		this.uri = uri;
		this.entityName = entityName;
		this.categoryFolder = categoryFolder;
	}

	public Entity(String uri) {
		this.name = "";
		this.uri = uri;
		this.entityName = "";
		this.categoryFolder = null;
	}

	public String getName() {
		return name;
	}

	public String getUri() {
		return uri;
	}

	public String getEntityName() {
		return entityName;
	}

	public Category getCategoryFolder() {
		return categoryFolder;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Entity [name=" + name + ", uri=" + uri + ", entityName=" + entityName + ", categoryFolder="
				+ categoryFolder + "]";
	}

}
