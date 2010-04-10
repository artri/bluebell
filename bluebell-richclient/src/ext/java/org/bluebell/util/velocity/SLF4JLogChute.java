package org.bluebell.util.velocity;

import java.text.MessageFormat;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a simple SLF4J system that will either latch onto an existing category, or just do a simple rolling
 * file log.
 * 
 * @author Mandus Elfving
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>: Implementation copied from <a
 *         href="https://jira.springsource.org/browse/SES-5">Spring JIRA</a> and modified according to Bluebell coding
 *         standards.
 */
public class SLF4JLogChute implements LogChute {

    /**
     * The runtime log SLF4J logger.
     */
    private static final String RUNTIME_LOG_SLF4J_LOGGER = "runtime.log.logsystem.slf4j.logger";

    /**
     * The init method debug message format.
     */
    private static final MessageFormat INIT_MESSAGE_FMT = new MessageFormat("SLF4JLogChute using logger \"{0}\"");

    /**
     * The SLF4L logger instance.
     */
    private Logger logger = null;

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.velocity.runtime.log.LogChute#init(org.apache.velocity.runtime.RuntimeServices)
     */
    public void init(RuntimeServices rs) throws Exception {

        final String name = (String) rs.getProperty(SLF4JLogChute.RUNTIME_LOG_SLF4J_LOGGER);
        if (name != null) {
            this.logger = LoggerFactory.getLogger(name);
            this.log(LogChute.DEBUG_ID, SLF4JLogChute.INIT_MESSAGE_FMT.format(new String[] { this.logger.getName() }));
        } else {
            this.logger = LoggerFactory.getLogger(this.getClass());
            this.log(LogChute.DEBUG_ID, SLF4JLogChute.INIT_MESSAGE_FMT.format(new Object[] { this.logger.getClass() }));
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.velocity.runtime.log.LogChute#log(int, java.lang.String)
     */
    public void log(int level, String message) {

        switch (level) {
            case LogChute.WARN_ID:
                this.logger.warn(message);
                break;
            case LogChute.INFO_ID:
                this.logger.info(message);
                break;
            case LogChute.TRACE_ID:
                this.logger.trace(message);
                break;
            case LogChute.ERROR_ID:
                this.logger.error(message);
                break;
            case LogChute.DEBUG_ID:
            default:
                this.logger.debug(message);
                break;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.velocity.runtime.log.LogChute#log(int, java.lang.String, java.lang.Throwable)
     */
    public void log(int level, String message, Throwable t) {

        switch (level) {
            case LogChute.WARN_ID:
                this.logger.warn(message, t);
                break;
            case LogChute.INFO_ID:
                this.logger.info(message, t);
                break;
            case LogChute.TRACE_ID:
                this.logger.trace(message, t);
                break;
            case LogChute.ERROR_ID:
                this.logger.error(message, t);
                break;
            case LogChute.DEBUG_ID:
            default:
                this.logger.debug(message, t);
                break;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.velocity.runtime.log.LogChute#isLevelEnabled(int)
     */
    public boolean isLevelEnabled(int level) {

        switch (level) {
            case LogChute.DEBUG_ID:
                return this.logger.isDebugEnabled();
            case LogChute.INFO_ID:
                return this.logger.isInfoEnabled();
            case LogChute.TRACE_ID:
                return this.logger.isTraceEnabled();
            case LogChute.WARN_ID:
                return this.logger.isWarnEnabled();
            case LogChute.ERROR_ID:
                return this.logger.isErrorEnabled();
            default:
                return Boolean.TRUE;
        }
    }
}
