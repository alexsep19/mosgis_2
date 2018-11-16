define ([], function () {
    
    var grid_name = 'voting_protocol_vote_initiators_grid'
    
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
                footer: true,
                toolbarColumns: true,
                toolbarAdd: true,
            },            

            textSearch: 'contains',

            searches: [            
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},
            ].filter (not_off),

            columns: 
            [                
                {field: 'protocolnum', caption: 'Номер протокола', size: 10, hidden: 1},
                {field: 'protocoldate', caption: 'Дата составления протокола', size: 7, render: _dt},
                {field: 'extravoting', caption: 'Вид собрания', size: 7, render: function (r) {return r.extravoting ? 'Внеочередное' : 'Ежегодное'}},
                {field: 'meetingeligibility', caption: 'Правомочность проведения собрания', size: 10, render: function (r) {return r.meetingeligibility == "C" ? 'Правомочное' : 'Неправомочное'}},
                {field: 'modification', caption: 'Основания изменения', size: 15, hidden: 1},
                {field: 'label_form', caption: 'Форма проведения', size: 15},
                {field: 'status_label', caption: 'Статус протокола', size: 10},
            ].filter (not_off),

            postData: {data: {"uuid_house": data.item.fiashouseguid}},

            url: '/mosgis/_rest/?type=voting_protocols',
           
            onDblClick: function (e) {
                openTab ('/voting_protocol/' + e.recid)
            },

            onAdd: $_DO.create_house_voting_protocols
            
        })

    }
    
})