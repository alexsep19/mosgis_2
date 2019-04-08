define ([], function () {
    
    var grid_name = 'overhaul_regional_program_files_grid'
    
    function getData () {
        return $('body').data ('data')
    }
            
    return function (data, view) {
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))
        
        var is_editable = data.item._can.edit

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                footer: 1,
                toolbarReload: false,
                toolbarColumns: false,
                toolbarInput: false,
                toolbarAdd: is_editable,
                toolbarDelete: is_editable,
            },            

            textSearch: 'contains',

            columns: [
                {field: 'label', caption: 'Наименование', size: 100},
                {field: 'len', caption: 'Объём, Мб', size: 10, render: function (r) {return (r.len/1024/1024).toFixed(3)}},
                {field: 'description', caption: 'Описание', size: 50},
            ],
            
            postData: {search: [
                {field: "uuid_oh_reg_pr_doc", operator: "is", value: $_REQUEST.id},
            ]},

            url: '/_back/?type=overhaul_regional_program_files',
            
            onDblClick: $_DO.download_overhaul_regional_program_files,
            
            onDelete: $_DO.delete_overhaul_regional_program_files,
            
            onAdd: $_DO.create_overhaul_regional_program_files,
                        
        })

    }
    
})