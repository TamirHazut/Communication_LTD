package demo.data;

import java.util.Date;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class UserEntity {
	@Id
	private String username;
	private String password;
	private String salt;
	private String email;
	private int numberOfLoginAttempt;
	@Temporal(TemporalType.TIMESTAMP)
	private Date creationTimestamp;
	@ElementCollection
	@CollectionTable(name = "old_passwords", joinColumns = @JoinColumn(referencedColumnName = "username"))
	@Column(name = "oldPasswords")
	private List<String> oldPasswords;
}
