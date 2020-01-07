package com.nature.repository.system;

import com.nature.component.system.model.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.io.Serializable;

public interface SysUserJpaRepository extends JpaRepository<SysUser, String>, JpaSpecificationExecutor<SysUser>, Serializable {

}
