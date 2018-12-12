define ([], function () {
    
    var grid_name = 'public_property_contract_voting_protocols_grid'
                
    return function (data, view) {

//        var permissions = data.item.id_prtcl_status_gis == 10 || data.item.id_prtcl_status_gis == 11

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                footer: 1,
                toolbarReload: false,
                toolbarColumns: false,
                toolbarInput: false,
                toolbarAdd: true,
                toolbarDelete: true,
            },            

            textSearch: 'contains',

            columns: [                               
                {field: 'label', caption: 'Наименование', size: 100},
                {field: 'len', caption: 'Объём, Мб', size: 15, render: function (r) {return (r.len/1024/1024).toFixed(3)}},
                {field: 'description', caption: 'Описание', size: 50},
            ],
            
            postData: {data: {uuid_ctr: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=public_property_contract_voting_protocols',
            
//            onDblClick: $_DO.download_public_property_contract_voting_protocols,
            
            onDelete: $_DO.delete_public_property_contract_voting_protocols,
            
            onAdd: $_DO.create_public_property_contract_voting_protocols,
                        
        })

    }
    
})