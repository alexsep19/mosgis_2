define ([], function () {
    
    var grid_name = 'public_property_contract_docs_grid'
    
    function getData () {
        return $('body').data ('data')
    }
            
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
                toolbarEdit: true,
            },            

            textSearch: 'contains',

            columns: [                               
                {field: 'label', caption: 'Наименование', size: 100},
                {field: 'id_type', caption: 'Тип', size: 30, voc: data.vc_pp_ctr_file_types},
                {field: 'protocolnum', caption: '№ протокола', size: 30},
                {field: 'protocoldate', caption: 'Дата протокола', size: 18, render: _dt},
                {field: 'len', caption: 'Объём, Мб', size: 15, render: function (r) {return (r.len/1024/1024).toFixed(3)}},
                {field: 'description', caption: 'Описание', size: 50},
            ],
            
            postData: {search: [
                {field: "uuid_ctr", operator: "is", value: $_REQUEST.id}
            ]},

            url: '/mosgis/_rest/?type=public_property_contract_docs',
            
            onDblClick: $_DO.download_public_property_contract_docs,
            
            onDelete: $_DO.delete_public_property_contract_docs,
            
            onAdd: $_DO.create_public_property_contract_docs,
            
            onEdit: $_DO.edit_public_property_contract_docs,
            
        })

    }
    
})