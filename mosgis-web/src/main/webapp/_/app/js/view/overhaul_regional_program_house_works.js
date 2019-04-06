define ([], function () {
    
    var grid_name = 'overhaul_regional_program_house_works_grid'
    
    function getData () {
        return $('body').data ('data')
    }

    function recalcToolbar (e) {e.done (function () {

        var data = getData ()

        var g = w2ui [grid_name]

        var t = g.toolbar

        t.disable ('editButton')
        t.disable ('deleteButton')

        if (g.getSelection ().length != 1 || data.item['program.is_deleted']) return

        var status = g.get (g.getSelection () [0]).id_orphw_status

        if (status == 10 || status == 14 || status == 34) t.enable ('editButton')
        if (status == 10 || status == 14) t.enable ('deleteButton') //|| status == 40

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
                        id: 'approveButton',
                        caption: 'Импортировать в ГИС ЖКХ',
                        onClick: $_DO.approve_overhaul_regional_program_house_works,
                        disabled: !(data.item['program.last_succesfull_status'] == -21) && !(data.item['program.last_succesfull_status'] == -31)
                    },
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
                {field: 'work', caption: 'Вид работы', size: 50, voc: data.vc_oh_wk_types},
                {field: 'code_nsi_218', caption: 'Группа видов работ', size: 50, voc: data.vc_nsi_218},
                {field: 'startyearmonth', caption: 'Начало выполнения', size: 20, render: _dt},
                {field: 'endyearmonth', caption: 'Окончание выполнения', size: 20, render: _dt},
                {field: 'id_orphw_status', caption: 'Статус ГИС', size: 20, voc: data.vc_gis_status},
                {field: 'import_err_text', caption: 'Ошибка импорта', size: 40, render: 
                    function (record) {
                        if (record['id_orphw_status'] == 40)
                            record['w2ui'] = {'style': 'background-color: #90EE90; color: black;'}
                        else if (record['import_err_text'] !== '')
                            record['w2ui'] = {'style': 'background-color: #F08080; color: white;'}
                        return record['import_err_text']
                }},
            ],
            
            postData: {house_uuid: $_REQUEST.id},

            url: '/_back/?type=overhaul_regional_program_house_works',
            
            onAdd: $_DO.create_overhaul_regional_program_house_works,

            onSelect: recalcToolbar,
            onUnselect: recalcToolbar
                        
        })

    }
    
})