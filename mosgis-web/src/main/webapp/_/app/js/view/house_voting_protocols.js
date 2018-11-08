define ([], function () {
    
    var grid_name = 'house_voting_protocols_grid'
    
    function getData () {
        return $('body').data ('data')
    }
    
    return function (data, view) {
        
        function Permissions () {
            
            if (data.cach && data.cach['org.uuid'] == $_USER.uuid_org && data.cach.id_ctr_status_gis != 110)
                return true
            return false
            
        }

        var layout = w2ui ['topmost_layout']
        
        var $panel = $(layout.el ('main'))

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                footer: true,
                toolbarColumns: true,
                toolbarAdd: Permissions (),
                //toolbarDelete: Permissions (),
            },            

            textSearch: 'contains',

            searches: [            
                {field: 'status_label',  caption: 'Статус протокола',  type: 'text'},
                {field: 'label_form',  caption: 'Форма собрания',  type: 'text'},
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},
            ].filter (not_off),

            columns: 
            [                
                {field: 'protocolnum', caption: 'Номер протокола', size: 10},
                {field: 'protocoldate', caption: 'Дата составления протокола', size: 7, render: _dt},
                {field: 'extravoting', caption: 'Вид собрания', size: 7, render: function (r) {return r.extravoting ? 'Внеочередное' : 'Ежегодное'}},
                {field: 'meetingeligibility', caption: 'Правомочность проведения собрания', size: 10, render: function (r) {return r.meetingeligibility == "C" ? 'Правомочное' : 'Неправомочное'}},
                {field: 'modification', caption: 'Основания изменения', size: 20},
                {field: 'label_form', caption: 'Форма проведения', size: 15},
                {field: 'status_label', caption: 'Статус протокола', size: 10},
            ].filter (not_off),
            
            postData: {data: {"uuid_house": $_REQUEST.id}},

            url: '/mosgis/_rest/?type=voting_protocols',
           
            //onDelete: $_DO.delete_house_voting_protocols,
            
            onAdd: $_DO.create_house_voting_protocols,
            
        })

    }
    
})