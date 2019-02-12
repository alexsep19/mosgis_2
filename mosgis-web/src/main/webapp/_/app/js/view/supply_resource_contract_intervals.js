define ([], function () {
    
    var grid_name = 'supply_resource_contract_intervals_grid'
                
    return function (data, view) {
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: 1,
                footer: 1,
                toolbarReload: false,
                toolbarColumns: false,
                toolbarInput: false,
                toolbarAdd: data.item._can.create_intervals
            },
            
            textSearch: 'contains',
            
            columns: [
                {field: 'code_vc_nsi_3', caption: 'Вид коммунальной услуги', size: 40, voc: data.vc_nsi_3},
                {field: 'code_vc_nsi_239', caption: 'Коммунальный ресурс', size: 40, voc: data.vc_nsi_239},
                {field: 'startdateandtime', caption: 'Начало перерыва', size: 20, render: _dt},
                {field: 'enddateandtime', caption: 'Окончание перерыва', size: 20, render: _dt},
                {field: 'id_ctr_status', caption: 'Статус', size: 10, voc: data.vc_gis_status}
            ],
            
            postData: {data: {uuid_sr_ctr: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=intervals',
                                    
            onDblClick: function (e) {openTab ('/interval/' + e.recid)},
            
            onAdd: $_DO.create_supply_resource_contract_intervals,
            
        })

    }
    
})