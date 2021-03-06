package com.nature.domain.process;

import com.nature.base.util.SessionUserUtil;
import com.nature.base.vo.UserVo;
import com.nature.component.process.model.ProcessStop;
import com.nature.repository.process.ProcessStopJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProcessStopDomain {

    @Autowired
    private ProcessStopJpaRepository processStopJpaRepository;

    public ProcessStop getProcessStopById(String id) {
        ProcessStop processStop = processStopJpaRepository.getOne(id);
        if (null == processStop || !processStop.getEnableFlag()) {
            processStop = null;
        }
        return processStop;
    }

    public Page<ProcessStop> getProcessStopListPage(int page, int size, String param) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "crtDttm"));
        boolean isAdmin = SessionUserUtil.isAdmin();
        if (isAdmin) {
            return processStopJpaRepository.getProcessStopListPage(null == param ? "" : param, pageRequest);
        } else {
            UserVo currentUser = SessionUserUtil.getCurrentUser();
            return processStopJpaRepository.getProcessStopListPage(currentUser.getUsername(), null == param ? "" : param, pageRequest);
        }
    }

    public ProcessStop saveOrUpdate(ProcessStop processStop) {
        return processStopJpaRepository.save(processStop);
    }

    public List<ProcessStop> saveOrUpdate(List<ProcessStop> processStopList) {
        return processStopJpaRepository.saveAll(processStopList);
    }

    public int updateEnableFlagById(String id, boolean enableFlag) {
        return processStopJpaRepository.updateEnableFlagById(id, enableFlag);
    }


}
