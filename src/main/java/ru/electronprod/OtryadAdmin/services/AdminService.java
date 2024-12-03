package ru.electronprod.OtryadAdmin.services;

import com.sun.management.OperatingSystemMXBean;
import java.io.File;
import java.lang.management.ManagementFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.security.AuthHelper;

/**
 * Manages the native admin account and provides information about the system
 */
@Slf4j
@Service
public class AdminService implements InitializingBean {
	OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
	@Value("${security.admin.login}")
	private String admin_login;
	@Value("${security.admin.password}")
	private String admin_password;
	@Autowired
	private AuthHelper auth;

	/**
	 * Checks the existence of admin account
	 */
	@Override
	public void afterPropertiesSet() {
		if (auth.exists(admin_login) == false) {
			log.info("Administrator profile not found, creating a new one...");
			User user = new User();
			user.setRole("ROLE_ADMIN");
			user.setLogin(admin_login);
			user.setPassword(admin_password);
			auth.register(user);
		}
		log.info("Admin registered. Use authorization data from application.properties file.");
	}

	/**
	 * Checks if this is the native administrator
	 * 
	 * @param person - User to check
	 * @return true - native, false - not
	 */
	public boolean isNativeAdmin(User person) {
		if (person.getLogin().equals(admin_login) && person.getRole().equals("ROLE_ADMIN")) {
			return true;
		}
		return false;
	}

	public double getFreeDiskSpace() {
		File disk = new File("/");
		return disk.getFreeSpace() / (1024.0 * 1024.0 * 1024.0);
	}

	public double getTotalDiskSpace() {
		File disk = new File("/");
		return disk.getTotalSpace() / (1024.0 * 1024.0 * 1024.0);
	}

	public double getUsableDiskSpace() {
		File disk = new File("/");
		return disk.getUsableSpace() / (1024.0 * 1024.0 * 1024.0);
	}

	public OperatingSystemMXBean getSystemInfoBean() {
		return osBean;
	}
}
