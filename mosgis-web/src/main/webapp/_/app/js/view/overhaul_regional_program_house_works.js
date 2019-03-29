define ([], function () {
    
    var grid_name = 'overhaul_regional_program_house_works_grid'
    
    function getData () {
        return $('body').data ('data')
    }

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui [grid_name]

        var t = g.toolbar

        t.disable ('editButton')
        t.disable ('deleteButton')

        if (g.getSelection ().length != 1) return

        var status = g.get (g.getSelection () [0]).id_orphw_status

        if (status == 10 || status == 11) t.enable ('editButton')
        if (status == 10 || status == 40) t.enable ('deleteButton')

    })}
            
    return function (data, view) {
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))
        
        var is_editable = data.item._can.edit

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            toolbar: {
                items: [
                    {
                        type: 'button', 
                        id: 'editButton', 
                        caption: 'Редактировать', 
                        onClick: $_DO.edit_overhaul_regional_program_house_works, 
                        icon: 'w2ui-icon-pencil', 
                        disabled: true
                    },
                    {
                        type: 'button',
                        id: 'deleteButton',
                        caption: 'Удалить',
                        onClick: $_DO.delete_overhaul_regional_program_house_works,
                        icon: 'w2ui-icon-cross',
                        disabled: true
                    },
                ]
            },

            show: {
                toolbar: true,
                footer: 1,
                toolbarReload: false,
                toolbarColumns: false,
                toolbarInput: false,
                toolbarAdd: is_editable,
            },

            columns: [
                {field: 'work', caption: 'Вид работы', size: 100, voc: data.vc_oh_wk_types},
                {field: 'code_nsi_218', caption: 'Группа видов работ', size: 100, voc: data.vc_nsi_218},
                {field: 'startyearmonth', caption: 'Начало выполнения', size: 20},
                {field: 'endyearmonth', caption: 'Окончание выполнения', size: 20},
            ],
            
            postData: {house_uuid: $_REQUEST.id},

            url: '/mosgis/_rest/?type=overhaul_regional_program_house_works',
            
            onAdd: $_DO.create_overhaul_regional_program_house_works,

            onSelect: recalcToolbar,
            onUnselect: recalcToolbar
                        
        })

    }
    
})