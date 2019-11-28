package com.seeyon.ctp.common.filemanager.dao;

public interface PartitionDAO {
    public java.util.List<com.seeyon.ctp.common.po.filemanager.Partition> findAll();

    /**
     * Persist the given transient instance, first assigning a generated
     * identifier. (Or using the current value of the identifier property if the
     * assigned generator is used.)
     * 
     * @param partition
     *            a transient instance of a persistent class
     * @return the class identifier
     */
    public void save(com.seeyon.ctp.common.po.filemanager.Partition partition);

    /**
     * Update the persistent state associated with the given identifier. An
     * exception is thrown if there is a persistent instance with the same
     * identifier in the current session.
     * 
     * @param partition
     *            a transient instance containing updated state
     */
    public void update(com.seeyon.ctp.common.po.filemanager.Partition partition);

    /**
     * Remove a persistent instance from the datastore. The argument may be an
     * instance associated with the receiving Session or a transient instance
     * with an identifier associated with existing persistent state.
     * 
     * @param id
     *            the instance ID to be removed
     */
    public void delete(long id);

}