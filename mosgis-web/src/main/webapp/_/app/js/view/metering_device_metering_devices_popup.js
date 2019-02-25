define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'org_work_form',

                record: data.record,

                fields : [          
                ],

            })
            
            var is_virgin = 1
            
            $('#required_services_container').w2regrid ({ 
            
                name: 'metering_devices_grid',
                
                show: {
                    toolbar: false,
                    footer: false,
                    selectColumn: true
                },     
                
                columns: [
                    {field: 'meteringdevicenumber', caption: '№ ПУ', size: 10},
                    {field: 'meteringdevicestamp', caption: 'Марка', size: 10},
                    {field: 'meteringdevicemodel', caption: 'Модель', size: 10},
                    {field: 'mask_vc_nsi_2', caption: 'Ресурс', size: 15, voc: data.vc_nsi_2},
                    {field: 'installationplace', caption: 'Установлен', size: 15, voc: {in: 'на входе', out: 'на выходе'}},
                ],
                
                records: data.meters,
                
                onRender: function (e) {
                    
                    var grid = this
                
                    e.done (function () {
//                        $.each (data.accs, function () {grid.select (this.id)})                        
                    })
                
                }                
            
            }).refresh ()

       })

    }

})