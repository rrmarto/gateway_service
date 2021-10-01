package com.chilterra.gateway.controller;

import com.chilterra.gateway.persistence.model.Device;
import com.chilterra.gateway.persistence.model.Gateway;
import com.chilterra.gateway.persistence.repository.GatewayRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/v1/gateways")
public class GatewayController {

    private final GatewayRepository gatewayRepository;

    @Autowired
    public GatewayController(GatewayRepository gatewayRepository) {
        this.gatewayRepository = gatewayRepository;
    }


    @Operation(summary = "Get all gateways")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All gateways",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Gateway.class))})
    })
    @GetMapping
    public List<Gateway> getAllGateways() {
        return this.gatewayRepository.findAll();
    }


    @Operation(summary = "Get gateway by serialNumber")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gateway found with the provided serial number",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Gateway.class))}),
            @ApiResponse(responseCode = "404", description = "Gateways with serialNumber provided not found",
                    content = @Content)
    })
    @GetMapping("/{serialNumber}")
    public Gateway getBySerialNumber(@PathVariable String serialNumber) {
        return this.findGatewayBySerialNumber(serialNumber);
    }


    @Operation(summary = "Create a new gateway")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The gateway was created",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Gateway.class))}),
            @ApiResponse(responseCode = "400", description = "Gateway with the serial number already exist",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "The maximum gateway devices number supported is exceeded",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "The ip is not a valid IPV4",
                    content = @Content),
    })
    @PostMapping
    public Gateway createGateway(@RequestBody Gateway gateway) {
        this.validateGateway(gateway);
        Optional<Gateway> gatewayOptional = this.gatewayRepository.findBySerialNumber(gateway.getSerialNumber());

        if (gatewayOptional.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Gateway with serialNumber " + gateway.getSerialNumber() + " already exist.");
        }
        return this.gatewayRepository.save(gateway);
    }


    @Operation(summary = "Add a new gateway device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The gateway device was added",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Gateway.class))}),
            @ApiResponse(responseCode = "400", description = "The maximum gateway devices number supported is exceeded",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Gateways with serialNumber provided not found",
                    content = @Content),
    })
    @PostMapping("/{gatewaySerialNumber}/devices")
    public Gateway addGatewayDevice(@PathVariable("gatewaySerialNumber") String gatewaySerialNumber, @RequestBody Device device) {
        synchronized (this) {
            Gateway gateway = this.findGatewayBySerialNumber(gatewaySerialNumber);
            if (gateway.getDevices().size() < 10) {
                device.setGateway(gateway);
                gateway.getDevices().add(device);
                return this.gatewayRepository.save(gateway);
            }
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The gateway has the maximum device number supported[10]");
        }
    }


    @Operation(summary = "Update gateway device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The gateway device was updated",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Gateway.class))}),
            @ApiResponse(responseCode = "404", description = "Gateways with serialNumber provided not found",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Device with id provided not found",
                    content = @Content),
    })
    @PatchMapping("/{gatewaySerialNumber}/devices/{deviceId}")
    public Gateway updateGatewayDeviceStatus(@PathVariable String gatewaySerialNumber, @PathVariable Long deviceId, @RequestBody Device device) {
        Gateway gateway = this.findGatewayBySerialNumber(gatewaySerialNumber);
        Device deviceToUpdate = this.getDeviceByGatewayAndDeviceNumber(gateway, deviceId);
        deviceToUpdate.setStatus(device.getStatus());
        return this.gatewayRepository.save(gateway);
    }


    @Operation(summary = "Delete gateway device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The gateway device was deleted",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Gateway.class))}),
            @ApiResponse(responseCode = "404", description = "Gateways with serialNumber provided not found",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Device with id provided not found",
                    content = @Content),
    })
    @DeleteMapping("/{gatewaySerialNumber}/devices/{deviceId}")
    public Gateway deleteGatewayDevice(@PathVariable String gatewaySerialNumber, @PathVariable Long deviceId) {
        Gateway gateway = this.findGatewayBySerialNumber(gatewaySerialNumber);
        Device deviceToDelete = this.getDeviceByGatewayAndDeviceNumber(gateway, deviceId);
        gateway.getDevices().remove(deviceToDelete);
        return this.gatewayRepository.save(gateway);
    }


    private Gateway findGatewayBySerialNumber(String serialNumber) {
        return this.gatewayRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gateway not found"));
    }

    private Device getDeviceByGatewayAndDeviceNumber(Gateway gateway, Long deviceId) {
        return gateway.getDevices().stream()
                .filter(device -> Objects.equals(device.getId(), deviceId))
                .findFirst()
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));
    }

    private void validateGateway(Gateway gateway) {
        List<Device> devicesDto = gateway.getDevices();
        if (!CollectionUtils.isEmpty(devicesDto) && devicesDto.size() > 10) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The maximum device number supported is 10");
        }
        if (!InetAddressValidator.getInstance().isValidInet4Address(gateway.getIpV4())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The ip is not a valid IPV4");
        }
    }
}
