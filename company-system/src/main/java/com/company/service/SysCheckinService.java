package com.company.service;

import com.company.model.dto.CheckinAuditDTO;
import com.company.model.dto.CheckinQueryDTO;
import com.company.model.dto.SupplementDTO;
import com.company.model.vo.CheckinVO;
import com.company.model.vo.PageVO;
import com.company.model.vo.StatsVO;

import java.util.Map;

public interface SysCheckinService {
    /** 员工打卡 */
    Map<String, Object> punch(Long userId, String type);
    /** 今日打卡状态 */
    Map<String, Object> getTodayStatus(Long userId);
    /** 管理员查询打卡记录 */
    PageVO<CheckinVO> page(CheckinQueryDTO dto);
    /** 员工查看自己的打卡记录 */
    PageVO<CheckinVO> myPage(Long userId, Integer page, Integer size, String month);
    /** 审核补卡 */
    void audit(Long id, CheckinAuditDTO dto);
    /** 员工申请补卡 */
    void supplement(Long userId, SupplementDTO dto);
    /** 考勤统计 */
    Map<String, Object> stats(String month, Long deptId);
    /** 手动修正打卡 */
    void correct(Long id, CheckinAuditDTO dto);
}
