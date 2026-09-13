package com.vasquez.reservas_backend.servicio.service;


import com.vasquez.reservas_backend.servicio.dto.ActualizarServicioRequest;
import com.vasquez.reservas_backend.servicio.dto.CrearServicioRequest;
import com.vasquez.reservas_backend.servicio.dto.ServicioResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServicioService {

    ServicioResponse crear(CrearServicioRequest request);

    ServicioResponse actualizar(Long id, ActualizarServicioRequest request);

    ServicioResponse buscarPorId(Long id);

    Page<ServicioResponse> listarActivos(Pageable pageable);

    void desactivar(Long id);
}