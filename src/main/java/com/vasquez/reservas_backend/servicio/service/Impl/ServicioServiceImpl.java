package com.vasquez.reservas_backend.servicio.service.Impl;


import com.vasquez.reservas_backend.config.CacheConfig;
import com.vasquez.reservas_backend.servicio.dto.ActualizarServicioRequest;
import com.vasquez.reservas_backend.servicio.dto.CrearServicioRequest;
import com.vasquez.reservas_backend.servicio.dto.ServicioResponse;
import com.vasquez.reservas_backend.servicio.entity.Servicio;
import com.vasquez.reservas_backend.servicio.repository.ServicioRepository;
import com.vasquez.reservas_backend.shared.exception.BusinessException;
import com.vasquez.reservas_backend.shared.exception.ResourceNotFoundException;
import com.vasquez.reservas_backend.servicio.service.ServicioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
@Service
@Transactional
public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioServiceImpl(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.SERVICIOS_ACTIVOS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.SERVICIO_POR_ID, allEntries = true)
    })
    @Override
    public ServicioResponse crear(CrearServicioRequest request) {
        if (servicioRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new BusinessException(
                    "Ya existe un servicio con ese nombre"
            );
        }

        Servicio servicio = new Servicio(
                request.nombre(),
                request.descripcion(),
                request.duracionMinutos()
        );

        Servicio guardado = servicioRepository.save(servicio);

        return ServicioResponse.desde(guardado);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.SERVICIOS_ACTIVOS, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.SERVICIO_POR_ID, allEntries = true)
    })
    @Override
    public ServicioResponse actualizar(
            Long id,
            ActualizarServicioRequest request
    ) {
        Servicio servicio = obtenerOFallar(id);

        boolean cambioDeNombre = !servicio.getNombre()
                .equalsIgnoreCase(request.nombre());

        if (cambioDeNombre
                && servicioRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new BusinessException(
                    "Ya existe un servicio con ese nombre"
            );
        }

        servicio.actualizar(
                request.nombre(),
                request.descripcion(),
                request.duracionMinutos()
        );

        Servicio guardado = servicioRepository.save(servicio);
        return ServicioResponse.desde(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheConfig.SERVICIOS_ACTIVOS,
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort"
    )
    public Page<ServicioResponse> listarActivos(Pageable pageable) {
        return servicioRepository.findByActivoTrue(pageable)
                .map(ServicioResponse::desde);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = CacheConfig.SERVICIOS_ACTIVOS,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = CacheConfig.SERVICIO_POR_ID,
                    allEntries = true
            )
    })
    public void desactivar(Long id) {
        Servicio servicio = obtenerOFallar(id);
        servicio.desactivar();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheConfig.SERVICIO_POR_ID,
            key = "#id"
    )
    public ServicioResponse buscarPorId(Long id) {
        return ServicioResponse.desde(obtenerOFallar(id));
    }

    private Servicio obtenerOFallar(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Servicio no encontrado con id " + id
                ));
    }
}