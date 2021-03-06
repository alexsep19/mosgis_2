define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'org_work_form',

                record: data.record,

                fields : [                                
                    {name: 'workname', type: 'text'},
                    {name: 'code_vc_nsi_56', type: 'list', options: {items: data.vc_nsi_56.items}},
                    {name: 'stringdimensionunit', type: 'combo', options: {items: data.vc_okei.items}},
                ],

            })
            
            var is_virgin = 1
            
            $('#required_services_container').w2regrid ({ 
            
                name: 'code_vc_nsi_67_grid',
                
                show: {
                    toolbar: false,
                    footer: false,
                    columnHeaders: false,
                    selectColumn: true
                },     
                
                columns: [
                    {field: 'label', caption: 'Наименование', size: 50},
                ],
                
                records: dia2w2uiRecords (data.vc_nsi_67.items),
                
                onRefresh: function () {
                
                    if (!is_virgin) return
                    
                    var grid = this
               
                    $.each (data.record.codes_nsi_67, function () {grid.select ('' + this)})

                    is_virgin = 0
                
                }                
            
            }).refresh ()

       })

    }

})