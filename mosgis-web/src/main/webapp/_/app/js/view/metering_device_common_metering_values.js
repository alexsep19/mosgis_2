define ([], function () {

    return function (data, view) {
    
        var it = data.item       
            
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'metering_device_common_metering_values_grid',
            
            multiSelect: false,
            
            toolbar: {
            
                items: data.resources.items.map (function (i) {
                    return {
                        type: 'button', 
                        id: 'resource_' + i.id,
                        caption: i.text, 
                        icon: 'w2ui-icon-plus', 
                        onClick: $_DO.create_metering_device_common_metering_values, 
                    }
                })
                
            },                        

            show: {
                toolbar: true,
                toolbarDelete: it._can.edit,
                toolbarEdit: it._can.edit,
                toolbarInput: false,
                footer: true,
            },     

            columns: [                

                {field: 'datevalue', caption: 'Дата', size: 18, render: _dt},

                {field: 'id_type', caption: 'Тип', size: 18, voc: data.vc_meter_value_types},
                {field: 'code_vc_nsi_2', caption: 'Ресурс', size: 18, off: it.mask_vc_nsi_2 < 17, voc: data.resources},

                {field: 'meteringvaluet1', caption: it.tariffcount > 1 ? 'Показание T1' : 'Показание', size: 50, render: 'float:7'},
                {field: 'meteringvaluet2', caption: 'Показание T2', size: 50, render: 'float:7', off: it.tariffcount < 2},
                {field: 'meteringvaluet3', caption: 'Показание T3', size: 50, render: 'float:7', off: it.tariffcount < 3},

            ].filter (not_off),

            postData: {data: {uuid_meter: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=metering_device_values',

            onEdit: $_DO.edit_metering_device_common_metering_values,
            onDelete: $_DO.delete_metering_device_common_metering_values,

        }).refresh ();

    }

})