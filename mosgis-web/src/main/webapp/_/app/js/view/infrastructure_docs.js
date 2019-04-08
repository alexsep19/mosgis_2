define ([], function () {
    
    var grid_name = 'infrastructure_docs_grid'
                
    return function (data, view) {

        var it = data.item

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
                toolbarAdd: it._can.edit,
                toolbarDelete: it._can.edit,
                toolbarEdit: it._can.edit,
            },            

            textSearch: 'contains',

            columns: [                               
                {field: 'label', caption: 'Наименование', size: 100},
                {field: 'id_type', caption: 'Тип', size: 30, voc: data.vc_infrastructure_file_types},
                {field: 'len', caption: 'Объём, Мб', size: 15, render: function (r) {return (r.len/1024/1024).toFixed(3)}},
                {field: 'description', caption: 'Описание', size: 50},
            ],
            
            postData: {search: [
                {field: "uuid_oki", operator: "is", value: $_REQUEST.id}
            ]},

            url: '/_back/?type=infrastructure_docs',
            
            onDblClick: $_DO.download_infrastructure_docs,
            
            onDelete: $_DO.delete_infrastructure_docs,
            
            onAdd: $_DO.create_infrastructure_docs,
            
            onEdit: $_DO.edit_infrastructure_docs,
            
        })

    }
    
})