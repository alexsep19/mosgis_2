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
/*            
                {field: 'type', caption: 'Тип', size: 100, render: function (r) {var v = idx [r.id_type]; return v ? v.label : ''}},
                {field: 'dt', caption: 'Дата', size: 18, render: function (r) {
                    var vv = idx [r.id_type]
                    if (!vv) return ''
                    var v = data.item ['f_' + vv.id_dt]
                    return !v ? '' : dt_dmy (v.substr (0, 10))}
                },
                {field: 'no', caption: '№', size: 25, render: function (r) {
                    var vv = idx [r.id_type]
                    if (!vv) return ''
                    return data.item ['f_' + vv.id_no]}
                },
*/                
                {field: 'label', caption: 'Имя файла', size: 100},
                {field: 'len', caption: 'Объём, Мб', size: 10, render: function (r) {return (r.len/1024/1024).toFixed(3)}},
                {field: 'note', caption: 'Прим.', size: 50},
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