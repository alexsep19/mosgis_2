define ([], function () {
    
    var grid_name = 'mgmt_contract_docs_grid'
    
    function getData () {
        return $('body').data ('data')
    }
            
    return function (data, view) {
    
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
                {field: 'id_type', caption: 'Тип', size: 50, voc: data.vc_contract_doc_types},
                {field: 'label', caption: 'Наименование', size: 100},
                {field: 'len', caption: 'Объём, Мб', size: 10, render: function (r) {return (r.len/1024/1024).toFixed(3)}},
                {field: 'description', caption: 'Описание', size: 50},
            ],
            
            postData: {search: {"uuid_contract": $_REQUEST.id}},

            url: '/mosgis/_rest/?type=contract_docs',
            
            onDblClick: $_DO.download_mgmt_contract_docs,
            
            onDelete: $_DO.delete_mgmt_contract_docs,
            
            onAdd: $_DO.create_mgmt_contract_docs,
            
            onEdit: $_DO.edit_mgmt_contract_docs,
                        
//            onChange: $_DO.patch_mgmt_contract_common,
            
        })

    }
    
})