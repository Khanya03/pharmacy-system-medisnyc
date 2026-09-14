package za.ac.cput.medisnyc.repository;


/* IRepository.java
   Generic repository interface
   Author: Phemelo Molefi (230255299)
   Date: 19 March 2026
*/


import java.util.List;

public interface IRepository<T, ID> {
    T create(T entity);
    T read(ID id);        // Simple return, no Optional
    T update(T entity);
    boolean delete(ID id);
    List<T> getAll();     // Required by rubric
}

