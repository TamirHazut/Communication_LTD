package demo.config;

import java.util.ArrayList;
import java.util.List;

public enum Permission {
	DB(1 << 1), GENERAL(1 << 2), MAIL(1 << 3), PASSWORD(1 << 4), SSL(1 << 5);
	
	private long id;
	
	Permission(long id) {
		setId(id);
	}
	
	private void setId(long id) {
		if (id % 2 != 0) {
			id = 0;
		}
		this.id = id;
	}
	
	public long getId() {
		return this.id;
	}
	
	public static List<Permission> getPermissions(long value) {
		List<Permission> permissions = new ArrayList<>();
		for (Permission p : Permission.values()) {
			if ((value & p.id) != 0) {
				permissions.add(p);
			}
		}
		return permissions;
		
	}

}
