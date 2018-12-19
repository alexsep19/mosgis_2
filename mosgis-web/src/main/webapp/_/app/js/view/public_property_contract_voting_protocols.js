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
                {field: 'protocolnum', caption: '№', size: 5},
                {field: 'protocoldate', caption: 'Дата', size: 7, render: _dt},
                {field: 'extravoting', caption: 'Вид собрания', size: 7, voc: data.vc_voting_types},
                {field: 'meetingeligibility', caption: 'Правомочность проведения собрания', size: 10, voc: data.vc_voting_eligibility},
                {field: 'form_', caption: 'Форма проведения', size: 15, voc: data.vc_voting_forms},
                {field: 'id_prtcl_status', caption: 'Статус', size: 10, voc: data.vc_gis_status},
            ],
            
            postData: {data: {uuid_ctr: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=public_property_contract_voting_protocols',
            
            onDblClick: function (e) {openTab ('/voting_protocol/' + e.recid)},
            
            onDelete: $_DO.delete_public_property_contract_voting_protocols,
            
            onAdd: $_DO.create_public_property_contract_voting_protocols,
                        
        })

    }
    
})