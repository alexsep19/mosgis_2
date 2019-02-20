define ([], function () {
    
    var grid_name = 'metering_device_docs_grid'
    
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
                {field: 'id_type', caption: 'Тип', size: 50, voc: data.vc_meter_file_types},
                {field: 'label', caption: 'Наименование', size: 100},
                {field: 'len', caption: 'Объём, Мб', size: 10, render: function (r) {return (r.len/1024/1024).toFixed(3)}},
                {field: 'description', caption: 'Описание', size: 50},
            ],
            
            postData: {search: [
                {field: "uuid_meter", operator: "is", value: $_REQUEST.id},
            ]},

            url: '/mosgis/_rest/?type=metering_device_docs',
            
            onDblClick: $_DO.download_metering_device_docs,
            
            onDelete: $_DO.delete_metering_device_docs,
            
            onAdd: $_DO.create_metering_device_docs,
                        
        })

    }
    
})