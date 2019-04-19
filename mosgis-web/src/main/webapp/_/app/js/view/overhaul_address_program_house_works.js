define ([], function () {
    
    var grid_name = 'overhaul_address_program_house_works_grid'
    
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

        console.log (g.get (g.getSelection () [0]))

        var status = g.get (g.getSelection () [0]).id_oaphw_status

        if (status == 10 || status == 14 || status == 34) t.enable ('editButton')
        if (status == 10 || status == 14 || status == 40) t.enable ('deleteButton')

    })}
            
    return function (data, view) {
    
        var layout = w2ui ['passport_layout']

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
                        caption: 'Отправить в ГИС ЖКХ',
                        onClick: $_DO.approve_overhaul_address_program_house_works,
                        disabled: !(data.item['program.last_succesfull_status'] == -21) && !(data.item['program.last_succesfull_status'] == -31)
                    },
                    {
                        type: 'button', 
                        id: 'editButton', 
                        caption: 'Редактировать', 
                        onClick: $_DO.edit_overhaul_address_program_house_works, 
                        icon: 'w2ui-icon-pencil', 
                        disabled: true
                    },
                    {
                        type: 'button',
                        id: 'deleteButton',
                        caption: 'Удалить',
                        onClick: $_DO.delete_overhaul_address_program_house_works,
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

            columnGroups : [            
                {span: 3, caption: 'Общие сведения'},                
                {span: 5, caption: 'Финансирование'},
                {span: 2, caption: 'Стоимость работы'},                    
                {span: 3, caption: 'ГИС ЖКХ'},                
            ],

            columns: [
                {field: 'work', caption: 'Вид работы', size: 30, voc: data.vc_oh_wk_types},
                {field: 'code_nsi_218', caption: 'Группа видов работ', size: 30, voc: data.vc_nsi_218},
                {field: 'endmonthyear', caption: 'Окончание выполнения', size: 20, render: _dt},
                {field: 'fund', caption: 'Средства Фонда ЖКХ', size: 20},
                {field: 'regionbudget', caption: 'Бюджет субъекта РФ', size: 20},
                {field: 'municipalbudget', caption: 'Местный бюджет', size: 20},
                {field: 'owners', caption: 'Средства собственников', size: 20},
                {field: 'total', caption: 'Всего', size: 20},
                {field: 'specificcost', caption: 'Удельная', size: 20},
                {field: 'maximumcost', caption: 'Предельная', size: 20},
                {field: 'guid', caption: 'Идентификатор', size: 30},
                {field: 'id_oaphw_status', caption: 'Статус', size: 20, voc: data.vc_gis_status},
                {field: 'import_err_text', caption: 'Ошибка импорта', size: 40, render: 
                    function (record) {
                        if (record['id_oaphw_status'] == 40)
                            record['w2ui'] = {'style': 'background-color: #90EE90; color: black;'}
                        else if (Math.abs (record['id_oaphw_status']) % 10 == 4)
                            record['w2ui'] = {'style': 'background-color: #F08080; color: white;'}
                        return record['import_err_text']
                }},
            ],
            
            postData: {house_uuid: $_REQUEST.id},

            url: '/_back/?type=overhaul_address_program_house_works',
            
            onAdd: $_DO.create_overhaul_address_program_house_works,

            onSelect: recalcToolbar,
            onUnselect: recalcToolbar
                        
        })

    }
    
})