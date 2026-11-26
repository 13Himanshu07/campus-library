package com.library.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.PreparedStatement;

/** Best-effort audit recorder; only identifiers and action metadata are stored. */
public final class AuditLog {
    private static final Logger LOG=LoggerFactory.getLogger(AuditLog.class);
    private AuditLog() { }
    public static void record(Long actor,String action,String entity,String entityId,boolean success) {
        try(var c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement("INSERT INTO audit_log(actor_user_id,action,entity_type,entity_id,outcome) VALUES(?,?,?,?,?)")) {
            if(actor==null)p.setNull(1,java.sql.Types.BIGINT);else p.setLong(1,actor);
            p.setString(2,action);p.setString(3,entity);p.setString(4,entityId);p.setString(5,success?"SUCCESS":"FAILURE");p.executeUpdate();
        } catch(Exception e) { LOG.error("audit_record_failed action={} entity={} exception={}",action,entity,e.getClass().getSimpleName()); }
    }
}
