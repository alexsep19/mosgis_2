define ([], function () {

    return function (data, view) {
    
        var it = data.item       
            
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'metering_device_metering_devices_grid',

            show: {
                toolbar: true,
                toolbarAdd: it._can.edit,
                toolbarDelete: it._can.edit,
                toolbarInput: false,
                footer: true,
            },     

            columns: [                
                {field: 'meter.meteringdevicenumber', caption: '№ ПУ', size: 20},
            ],

            records: data.meters,

            onAdd: $_DO.create_metering_device_metering_devices,

            onDelete: $_DO.delete_metering_device_metering_devices,

        }).refresh ();

    }

})